package com.mvs.visualize;

import com.mvs.app.verification.report.VerificationReport;
import com.mvs.cfg.build.mvsCFG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddMoreInformation {
    mvsCFG mvsCFG;
    VerificationReport verificationReport;

    public AddMoreInformation(mvsCFG mvsCFG, VerificationReport verificationReport) {
        this.mvsCFG = mvsCFG;
        this.verificationReport = verificationReport;
    }

    public Map<String, String> parseParameters(){
        Map<String, String> listParameters = new HashMap<>();
        List<String> result = this.verificationReport.getResult();
        Pattern pattern = Pattern.compile("define-fun\\s([\\w|\\d|-]+)\\s");
        for(int i=0;i<result.size();i++){
            String line = result.get(i);
            if(line.contains("(define-fun")){
                Matcher matcher = pattern.matcher(line);
                if(matcher.find()){
                    String parameter_name = matcher.group(1);
                    i+=1;
                    String valueLine = result.get(i);
                    String value = valueLine.replace('(', ' ')
                            .replace(')', ' ')
                            .trim();
                    listParameters.put(parameter_name, value);
                }
            }
        }
        return listParameters;
    }

}
