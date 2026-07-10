package com.alpro.physio.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ToyyibPayService {

    @Value("${toyyibpay.base-url}")
    private String baseUrl;

    @Value("${toyyibpay.user-secret-key}")
    private String userSecretKey;

    @Value("${toyyibpay.category-code}")
    private String categoryCode;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createBill(String billName, String billDescription, int billAmountInCents,
                              String returnUrl, String callbackUrl, String externalRefNo,
                              String payerName, String payerEmail, String payerPhone) {

        String sanitizedName = billName.replaceAll("[^a-zA-Z0-9 _]", "").trim();
        if (sanitizedName.length() > 30) {
            sanitizedName = sanitizedName.substring(0, 30);
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userSecretKey", userSecretKey);
        params.add("categoryCode", categoryCode);
        params.add("billName", sanitizedName);
        params.add("billDescription", billDescription.length() > 100 ? billDescription.substring(0, 100) : billDescription);
        params.add("billPriceSetting", "1");
        params.add("billPayorInfo", "1");
        params.add("billAmount", String.valueOf(billAmountInCents));
        params.add("billReturnUrl", returnUrl);
        params.add("billCallbackUrl", callbackUrl);
        params.add("billExternalReferenceNo", externalRefNo);
        params.add("billTo", payerName);
        params.add("billEmail", payerEmail);
        params.add("billPhone", payerPhone);
        params.add("billPaymentChannel", "0");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            // Fetch as raw String — ToyyibPay's server mislabels its
            // Content-Type header as text/html even though the body is
            // real JSON, which breaks Spring's automatic message conversion.
            // Parsing manually with ObjectMapper sidesteps that entirely.
            String rawResponse = restTemplate.postForObject(
                    baseUrl + "/index.php/api/createBill", request, String.class);

            List<Map<String, Object>> parsed = objectMapper.readValue(
                    rawResponse.trim(), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});

            if (parsed == null || parsed.isEmpty() || !parsed.get(0).containsKey("BillCode")) {
                throw new RuntimeException("ToyyibPay did not return a valid BillCode. Raw response: " + rawResponse);
            }

            return (String) parsed.get(0).get("BillCode");

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ToyyibPay response as JSON.", e);
        }
    }

    public String getPaymentUrl(String billCode) {
        return baseUrl + "/" + billCode;
    }

    public boolean verifyBillPaidSuccessfully(String billCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("billCode", billCode);
        params.add("userSecretKey", userSecretKey); // NOTE: verify this is the correct param name against your sandbox

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            String rawResponse = restTemplate.postForObject(
                    baseUrl + "/index.php/api/getBillTransactions", request, String.class);

            List<Map<String, Object>> transactions = objectMapper.readValue(
                    rawResponse.trim(), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});

            if (transactions == null || transactions.isEmpty()) {
                return false;
            }

            Object status = transactions.get(0).get("billpaymentStatus");
            return status != null && "1".equals(status.toString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to verify bill payment status.", e);
        }
    }
}