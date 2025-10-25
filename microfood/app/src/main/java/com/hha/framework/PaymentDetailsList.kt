package com.hha.framework

import com.hha.common.PaymentStatus
import com.hha.daily.payment.PaymentDetailsList
import com.hha.types.CMoney

//payment_transaction CsqlDailyTransactionPaymentIterator::selectTransactionId(
//long transactionId, bool includeCancelledPayments)
//{
//   auto& stub = stubSqlDailyTransactionPayment();
//   grpc::ClientContext context;
//
//   dtp::TransactionPaymentRequest request;
//   dtp::PaymentDetailsList response;
//
//   request.set_transaction_id(transactionId);
//   request.set_include_cancelled_payments(includeCancelledPayments);
//
//   SQL_LOG_GRPC(stub.SelectTransactionId(&context, request, &response));
//
//   payment_transaction pay;
//
//   for (int n = 0; n < response.payment_details_size(); n++)
//   {
//      auto p = response.payment_details(n);
//      Epayed paid = convertPaidStatus(p.is_paid());
//      EpaymentMethod method = convertPaymentMethod(p.payment_method());
//      Cmoney m(p.total());
//      pay.add_payment(paid, method, m);
//   }
//   return pay;
