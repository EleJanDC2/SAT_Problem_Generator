package pl.janczaja;

import java.util.*;

public class LogicToken {

    public final ArrayList<String> relation_types = new ArrayList<>(List.of("OR"));
    public final ArrayList<String> special_types = new ArrayList<>(Arrays.asList("EXISTS","FORALL","ATOM","IMP"));

    String tokenType;
    String value;
    Boolean negation;

    Random random;

    LogicToken(){
        random = new Random();
        this.tokenType = relation_types.get(random.nextInt(relation_types.size()));
        value = null;
        negation = null;
    }

    LogicToken(String tokentype, Optional<String> value_optional, Optional<Boolean> negation_optional){
        this.random = new Random();
        //
        this.tokenType = tokentype;
        // Wartości domyślne
        this.value = value_optional.orElse(null);
        this.negation = negation_optional.orElse(null);

        if(!(relation_types.contains(tokentype) || special_types.contains(tokentype))){
            throw new RuntimeException("LogicToken.__init__: Token type not accepted.");
        }

        if(tokentype.equals("ATOM") && (value == null || negation == null)){
            throw new RuntimeException("LogicToken.__init__: ATOM token must have a value and negation flag set.");
        }
    }

    public String toString(){
        String result = "";

        if(tokenType.equals("ATOM")){
            result = this.value;
            if(this.negation){
                result = "-" + result;
            }
        } else {
            result = this.tokenType;
        }

        return result;
    }

    //


    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getNegation() {
        return negation;
    }

    public void setNegation(Boolean negation) {
        this.negation = negation;
    }
}
