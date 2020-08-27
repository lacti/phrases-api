package io.github.lacti.okt;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.phrase_extractor.KoreanPhraseExtractor;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = Logger.getLogger(Handler.class);

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        if (!input.containsKey("body")
                || input.get("body") == null
                || !String.class.equals(input.get("body").getClass())
                || ((String) input.get("body")).length() == 0) {
            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .build();
        }

        final String text = (String) input.get("body");
        final long startMillis = System.currentTimeMillis();
        final List<CharSequence> result = phrases(text);
        LOG.info(String.format("len(text)=%d, len(result)=%d, elapsed=%dms"
                , text.length(), result.size(), System.currentTimeMillis() - startMillis));
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(phrases(text))
                .setHeaders(new HashMap<String, String>() {{
                    put("Access-Control-Allow-Origin", "*");
                    put("Access-Control-Allow-Methods", "POST");
                }})
                .build();
    }

    // Copy from https://github.com/konlpy/konlpy/blob/master/konlpy/java/src/kr/lucypark/okt/OktInterface.java
    public static List<CharSequence> phrases(String string) {
        final Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(string);
        List<KoreanPhraseExtractor.KoreanPhrase> phrases = OpenKoreanTextProcessorJava.extractPhrases(tokens, false, false);

        List<CharSequence> list = new ArrayList<>();
        for (KoreanPhraseExtractor.KoreanPhrase phrase : phrases) {
            final CharSequence tmpCharSeq = phrase.text();
            list.add(tmpCharSeq);
        }
        return list;
    }
}
