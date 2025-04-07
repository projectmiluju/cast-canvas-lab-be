package com.castcanvaslab.api.common.global.error;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
	String code,
	String message,
	Map<String, String> details,
	OffsetDateTime timestamp
) {

	public static ErrorResponse of(ErrorCode errorCode, Map<String, String> details) {
		return new ErrorResponse(
			errorCode.code(),
			errorCode.message(),
			details,
			OffsetDateTime.now()
		);
	}
}
