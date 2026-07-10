package com.alpro.physio.controller.participant;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;
import com.alpro.physio.service.ToyyibPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/participant/payment")
public class EnrollmentPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentPaymentController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @Autowired
    private ToyyibPayService toyyibPayService;

    @Value("${toyyibpay.return-url}")
    private String returnUrl;

    @Value("${toyyibpay.callback-url}")
    private String callbackUrl;

    /**
     * Called by the logged-in participant's browser to start a payment.
     * Requires normal session auth.
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(HttpServletRequest request, @RequestBody Map<String, Integer> body) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Integer courseId = body.get("courseId");
            if (courseId == null) {
                response.put("status", "error");
                response.put("message", "courseId is required.");
                return ResponseEntity.badRequest().body(response);
            }

            HttpSession session = request.getSession(false);
            String userId = (String) session.getAttribute("userId");

            CourseDTO course = dao.courseDAO().getCourseById(courseId);
            if (course == null) {
                response.put("status", "error");
                response.put("message", "Course not found.");
                return ResponseEntity.status(404).body(response);
            }

            UserDTO user = dao.userDAO().findByUserId(userId);

            BigDecimal price = course.getCoursePrice();
            int amountInCents = price.multiply(BigDecimal.valueOf(100)).intValue();

            String externalRefNo = "ENR-" + UUID.randomUUID().toString().substring(0, 12);

            String billCode = toyyibPayService.createBill(
                    course.getCourseName(),
                    "Enrollment payment for " + course.getCourseName(),
                    amountInCents,
                    returnUrl,
                    callbackUrl,
                    externalRefNo,
                    user.getFullname(),
                    user.getEmail(),
                    "0000000000" // TODO: no phone field confirmed on UserDTO — placeholder, ToyyibPay requires this field
            );

            dao.paymentTransactionDAO().insertPendingTransaction(billCode, userId, courseId, price);

            response.put("status", "success");
            response.put("paymentUrl", toyyibPayService.getPaymentUrl(billCode));

        } catch (Exception e) {
            logger.error("Failed to initiate payment", e);
            response.put("status", "error");
            response.put("message", "Failed to initiate payment.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }

    /**
     * ToyyibPay calls this server-to-server after payment completes.
     * MUST stay public (no authService.validateAuth) — ToyyibPay's server
     * has no session/bearer token. Security relies entirely on
     * independently re-verifying via getBillTransactions below, since
     * ToyyibPay's callback itself carries no signature.
     */
    @PostMapping("/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam("billcode") String billCode,
            @RequestParam(value = "status", required = false) String status) {

        try {
            logger.info("Received ToyyibPay callback for billCode: {}, claimed status: {}", billCode, status);

            boolean actuallyPaid = toyyibPayService.verifyBillPaidSuccessfully(billCode);

            if (!actuallyPaid) {
                logger.warn("Callback claimed payment for billCode {} but independent verification failed.", billCode);
                dao.paymentTransactionDAO().updateStatus(billCode, 2);
                return ResponseEntity.ok().build();
            }

            var transaction = dao.paymentTransactionDAO().findByBillCode(billCode);
            if (transaction == null) {
                logger.error("No matching payment_transaction found for billCode: {}", billCode);
                return ResponseEntity.ok().build();
            }

            if (transaction.getStatus() == 1) {
                // Already processed — ToyyibPay may retry callbacks; this
                // guards against inserting a duplicate enrollment.
                return ResponseEntity.ok().build();
            }

            dao.paymentTransactionDAO().updateStatus(billCode, 1);
            dao.enrollCourseDAO().enrollParticipant(transaction.getUserId(), transaction.getCourseId());

            logger.info("Enrollment completed for userId: {}, courseId: {}", transaction.getUserId(), transaction.getCourseId());

        } catch (Exception e) {
            logger.error("Failed to process payment callback for billCode: {}", billCode, e);
        }

        return ResponseEntity.ok().build();
    }
}