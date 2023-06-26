package org.sensetimeframework.statistics.formula;

import org.sensetimeframework.statistics.entity.ConfusionMatrix;
import org.sensetimeframework.statistics.exception.ParamIllegalException;
import org.sensetimeframework.statistics.message.Message;

public class AdvancedCalculator {
    public static ConfusionMatrix adjustPnRate(ConfusionMatrix confusionMatrix,Integer positive,Integer negative) throws ParamIllegalException {
        return adjustRate(confusionMatrix.getPositivePart(),confusionMatrix.getNegativePart(),positive,negative);
    }

    public static ConfusionMatrix adjustRate(ConfusionMatrix confusionMatrix1,ConfusionMatrix confusionMatrix2,Integer share1,Integer share2) throws ParamIllegalException {
        if(share1 < 0 || share2 < 0 || confusionMatrix1 == null || confusionMatrix2 == null){
            throw new ParamIllegalException(Message.PARAM_ILLEGAL);
        }

        if(share1 == 0 && share2 == 0){
            return SimpleCalculator.add(confusionMatrix1,confusionMatrix2);
        } else if(share1 == 0){
            return confusionMatrix2.deepCopy();
        } else if(share2 == 0){
            return confusionMatrix1.deepCopy();
        } else {
            long validSize1 = confusionMatrix1.getValidSize();
            long validSize2 = confusionMatrix2.getValidSize();

            if(validSize1 == 0L || validSize2 == 0L){
                return new ConfusionMatrix(0L,0L,0L,0L,0L,0L);
            } else {
                long multiple1 = share1 * validSize2;
                long multiple2 = share2 * validSize1;
                ConfusionMatrix resConfusionMatrix1 = SimpleCalculator.multiply(confusionMatrix1,multiple1);
                ConfusionMatrix resConfusionMatrix2 = SimpleCalculator.multiply(confusionMatrix2,multiple2);

                return SimpleCalculator.add(resConfusionMatrix1,resConfusionMatrix2);
            }
        }
    }
}
