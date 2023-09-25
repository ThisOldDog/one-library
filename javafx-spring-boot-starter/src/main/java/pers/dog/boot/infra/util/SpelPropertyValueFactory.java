package pers.dog.boot.infra.util;

import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author 废柴 2023/9/25 19:57
 */
public class SpelPropertyValueFactory<S, T> implements Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {
    private static final Logger logger = LoggerFactory.getLogger(SpelPropertyValueFactory.class);
    private final String expression;
    private final ExpressionParser parser = new SpelExpressionParser();

    public SpelPropertyValueFactory(@NamedArg("expression") String expression) {
        this.expression = expression;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservableValue<T> call(TableColumn.CellDataFeatures<S, T> param) {
        return getCellDataReflectively(param.getValue());
    }

    /**
     * Returns the property name provided in the constructor.
     *
     * @return the property name provided in the constructor
     */
    public final String getExpression() {
        return expression;
    }

    private ObservableValue<T> getCellDataReflectively(S rowData) {
        if (getExpression() == null || getExpression().isEmpty() || rowData == null) return null;
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("row", rowData);
        return new ReadOnlyObjectWrapper<T>((T) parser.parseExpression(getExpression())
                .getValue(context));
    }
}