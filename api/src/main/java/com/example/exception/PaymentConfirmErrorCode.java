package com.example.exception;

import com.example.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PaymentConfirmErrorCode implements ErrorCode {

    ALREADY_PROCESSED_PAYMENT("400_1", "이미 처리된 결제 입니다."),
    PROVIDER_ERROR("400_2", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    EXCEED_MAX_CARD_INSTALLMENT_PLAN("400_3", "설정 가능한 최대 할부 개월 수를 초과했습니다."),
    INVALID_REQUEST("400_4", "잘못된 요청입니다."),
    NOT_ALLOWED_POINT_USE("400_5", "포인트 사용이 불가한 카드로 카드 포인트 결제에 실패했습니다."),
    INVALID_API_KEY("400_6", "잘못된 시크릿키 연동 정보 입니다."),
    INVALID_REJECT_CARD("400_7", "카드 사용이 거절되었습니다. 카드사 문의가 필요합니다."),
    BELOW_MINIMUM_AMOUNT("400_8", "신용카드는 결제금액이 100원 이상, 계좌는 200원이상부터 결제가 가능합니다."),
    INVALID_CARD_EXPIRATION("400_9", "카드 정보를 다시 확인해주세요. (유효기간)"),
    INVALID_STOPPED_CARD("400_10", "정지된 카드 입니다."),
    EXCEED_MAX_DAILY_PAYMENT_COUNT("400_11", "하루 결제 가능 횟수를 초과했습니다."),
    NOT_SUPPORTED_INSTALLMENT_PLAN_CARD_OR_MERCHANT("400_12", "할부가 지원되지 않는 카드 또는 가맹점 입니다."),
    INVALID_CARD_INSTALLMENT_PLAN("400_13", "할부 개월 정보가 잘못되었습니다."),
    NOT_SUPPORTED_MONTHLY_INSTALLMENT_PLAN("400_14", "할부가 지원되지 않는 카드입니다."),
    EXCEED_MAX_PAYMENT_AMOUNT("400_15", "하루 결제 가능 금액을 초과했습니다."),
    NOT_FOUND_TERMINAL_ID("400_16", "단말기번호(Terminal Id)가 없습니다. 토스페이먼츠로 문의 바랍니다."),
    INVALID_AUTHORIZE_AUTH("400_17", "유효하지 않은 인증 방식입니다."),
    INVALID_CARD_LOST_OR_STOLEN("400_18", "분실 혹은 도난 카드입니다."),
    RESTRICTED_TRANSFER_ACCOUNT("400_19", "계좌는 등록 후 12시간 뒤부터 결제할 수 있습니다. 관련 정책은 해당 은행으로 문의해주세요."),
    INVALID_CARD_NUMBER("400_20", "카드번호를 다시 확인해주세요."),
    INVALID_UNREGISTERED_SUBMALL("400_21", "등록되지 않은 서브몰입니다. 서브몰이 없는 가맹점이라면 안심클릭이나 ISP 결제가 필요합니다."),
    NOT_REGISTERED_BUSINESS("400_22", "등록되지 않은 사업자 번호입니다."),
    EXCEED_MAX_ONE_DAY_WITHDRAW_AMOUNT("400_23", "1일 출금 한도를 초과했습니다."),
    EXCEED_MAX_ONE_TIME_WITHDRAW_AMOUNT("400_24", "1회 출금 한도를 초과했습니다."),
    CARD_PROCESSING_ERROR("400_25", "카드사에서 오류가 발생했습니다."),
    EXCEED_MAX_AMOUNT("400_26", "거래금액 한도를 초과했습니다."),
    INVALID_ACCOUNT_INFO_RE_REGISTER("400_27", "유효하지 않은 계좌입니다. 계좌 재등록 후 시도해주세요."),
    NOT_AVAILABLE_PAYMENT("400_28", "결제가 불가능한 시간대입니다"),
    UNAPPROVED_ORDER_ID("400_29", "아직 승인되지 않은 주문번호입니다."),
    EXCEED_MAX_MONTHLY_PAYMENT_AMOUNT("400_30", "당월 결제 가능금액인 1,000,000원을 초과 하셨습니다."),
    UNAUTHORIZED_KEY("401_1", "인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다."),
    REJECT_ACCOUNT_PAYMENT("403_1", "잔액부족으로 결제에 실패했습니다."),
    REJECT_CARD_PAYMENT("403_2", "한도초과 혹은 잔액부족으로 결제에 실패했습니다."),
    REJECT_CARD_COMPANY("403_3", "결제 승인이 거절되었습니다."),
    FORBIDDEN_REQUEST("403_4", "허용되지 않은 요청입니다."),
    REJECT_TOSSPAY_INVALID_ACCOUNT("403_5", "선택하신 출금 계좌가 출금이체 등록이 되어 있지 않아요. 계좌를 다시 등록해 주세요."),
    EXCEED_MAX_AUTH_COUNT("403_6", "최대 인증 횟수를 초과했습니다. 카드사로 문의해주세요."),
    EXCEED_MAX_ONE_DAY_AMOUNT("403_7", "일일 한도를 초과했습니다."),
    NOT_AVAILABLE_BANK("403_8", "은행 서비스 시간이 아닙니다."),
    INVALID_PASSWORD("403_9", "결제 비밀번호가 일치하지 않습니다."),
    INCORRECT_BASIC_AUTH_FORMAT("403_10", "잘못된 요청입니다. ':' 를 포함해 인코딩해주세요."),
    FDS_ERROR("403_11", "[토스페이먼츠] 위험거래가 감지되어 결제가 제한됩니다. 발송된 문자에 포함된 링크를 통해 본인인증 후 결제가 가능합니다. (고객센터: 1644-8051)"),
    NOT_FOUND_PAYMENT("404_1", "존재하지 않는 결제 정보 입니다."),
    NOT_FOUND_PAYMENT_SESSION("404_2", "결제 시간이 만료되어 결제 진행 데이터가 존재하지 않습니다."),
    FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING("500_1", "결제가 완료되지 않았어요. 다시 시도해주세요."),
    FAILED_INTERNAL_SYSTEM_PROCESSING("500_2", "내부 시스템 처리 작업이 실패했습니다. 잠시 후 다시 시도해주세요."),
    UNKNOWN_PAYMENT_ERROR("500_3", "결제에 실패했어요. 같은 문제가 반복된다면 은행이나 카드사로 문의해주세요."),
    PAYMENT_CONFIRM_ERROR_SERVER_ERROR("500_4", "결제 과정에서 서버 에러가 발생했습니다. 관리자에게 문의해주세요.");

    private final String code;
    private final String message;

    public static PaymentConfirmErrorCode findByName(String name) {
        return Arrays.stream(values())
                .filter(v -> v.name().equals(name))
                .findAny()
                .orElse(PAYMENT_CONFIRM_ERROR_SERVER_ERROR);
    }
}
