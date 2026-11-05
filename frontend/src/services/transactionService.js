import apiClient from "../config/axios.js";
import axios from "axios";

function serializeQueryParams(params) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.forEach((item) => searchParams.append(key, item));
    } else if (value !== undefined && value !== null && value !== "") {
      searchParams.append(key, value);
    }
  });

  return searchParams.toString();
}

export async function getTransactions(params) {
  const response = await apiClient.get("/api/v1/transactions", {
    params,
    paramsSerializer: { serialize: serializeQueryParams },
  });

  return response.data;
}

export async function getBankAccounts() {
  const response = await apiClient.get("/api/v1/bankaccount");
  return response.data;
}

export async function transferPayment(payload, idempotencyKey) {
  try {
    const response = await apiClient.post("/api/v1/payments/transfer", payload, {
      headers: { "idempotency-key": idempotencyKey },
    });

    return response.data;
  } catch (error) {
    if (!axios.isAxiosError(error)) {
      throw error instanceof Error ? error : new Error("Unable to send payment.");
    }

    const responseData = error.response?.data;
    const serverMessage =
      typeof responseData === "string"
        ? responseData
        : responseData && typeof responseData === "object"
          ? responseData.message || responseData.error || responseData.detail
          : null;

    throw new Error(
      typeof serverMessage === "string" && serverMessage
        ? serverMessage
        : error.response
          ? `Payment failed (${error.response.status}). Please try again.`
          : error.message,
      { cause: error },
    );
  }
}
