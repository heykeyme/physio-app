package com.alpro.physio.controller.participant;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/participant/payment")
public class PaymentReturnController {

    /**
     * The user's browser lands here after completing (or cancelling) payment
     * on ToyyibPay. These query params are NOT trustworthy — anyone could
     * type this URL with status_id=1 manually. This endpoint is purely
     * cosmetic; real enrollment only happens via the server-to-server
     * /callback endpoint, independently verified against ToyyibPay's API.
     */
    @GetMapping("/return")
    public void handleReturn(
            @RequestParam(required = false) String status_id,
            @RequestParam(required = false) String billcode,
            @RequestParam(required = false) String order_id,
            HttpServletResponse response) throws IOException {

        // Redirect back into the participant app with a hint for the
        // frontend to show a "processing" message — adjust the target path
        // below to match wherever your participant dashboard actually lives.
        response.sendRedirect("/physio/participant?paymentStatus=" + (status_id != null ? status_id : "unknown"));
    }
}