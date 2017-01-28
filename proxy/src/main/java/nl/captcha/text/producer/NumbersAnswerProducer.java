package nl.captcha.text.producer;


/**
 * TextProducer implementation that will return a series of numbers.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class NumbersAnswerProducer implements TextProducer {

    private static final int DEFAULT_LENGTH = 4;
    private static final char[] NUMBERS = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9' };

    private final TextProducer _txtProd;
    
    public NumbersAnswerProducer() {
        this(DEFAULT_LENGTH);
    }
    
    public NumbersAnswerProducer(int length) {
        _txtProd = new DefaultTextProducer(length, NUMBERS);
    }

    @Override public String getText() {
        return new StringBuffer(_txtProd.getText()).toString();
    }
}
