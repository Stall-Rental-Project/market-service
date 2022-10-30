package com.srs.market.dto;

import com.srs.common.ErrorCode;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateResult implements Serializable {
    private ErrorCode errorCode;
    private List<String> errorMessages;
    private Object item;

    public boolean hasError(){
        return errorCode != null;
    }

    public String getFirstErrorMessage(){
        if (errorMessages.size() > 0){
            return errorMessages.get(0);
        }
        return "";
    }
}
