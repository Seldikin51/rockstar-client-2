package moscow.rockstar.utility.math.calculator.shuntingyard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import moscow.rockstar.utility.math.calculator.function.Function;
import moscow.rockstar.utility.math.calculator.operator.Operator;
import moscow.rockstar.utility.math.calculator.tokenizer.OperatorToken;
import moscow.rockstar.utility.math.calculator.tokenizer.Token;
import moscow.rockstar.utility.math.calculator.tokenizer.Tokenizer;

public class ShuntingYard {
   public static Token[] convertToRPN(
      String expression, Map<String, Function> userFunctions, Map<String, Operator> userOperators, Set<String> variableNames, boolean implicitMultiplication
   ) {
      Stack<Token> stack = new Stack<>();
      List<Token> output = new ArrayList<>();
      Tokenizer tokenizer = new Tokenizer(expression, userFunctions, userOperators, variableNames, implicitMultiplication);

      label87:
      while (tokenizer.hasNext()) {
         Token token = tokenizer.nextToken();
         switch (token.getType()) {
            case 1:
            case 6:
               output.add(token);
               break;
            case 2:
               while (true) {
                  if (!stack.empty() && stack.peek().getType() == 2) {
                     OperatorToken o1 = (OperatorToken)token;
                     OperatorToken o2 = (OperatorToken)stack.peek();
                     if ((o1.getOperator().getNumOperands() != 1 || o2.getOperator().getNumOperands() != 2)
                        && (
                           o1.getOperator().isLeftAssociative() && o1.getOperator().getPrecedence() <= o2.getOperator().getPrecedence()
                              || o1.getOperator().getPrecedence() < o2.getOperator().getPrecedence()
                        )) {
                        output.add(stack.pop());
                        continue;
                     }
                  }

                  stack.push(token);
                  continue label87;
               }
            case 3:
               stack.add(token);
               break;
            case 4:
               stack.push(token);
               break;
            case 5:
               while (stack.peek().getType() != 4) {
                  output.add(stack.pop());
               }

               stack.pop();
               if (!stack.isEmpty() && stack.peek().getType() == 3) {
                  output.add(stack.pop());
               }
               break;
            case 7:
               while (!stack.empty() && stack.peek().getType() != 4) {
                  output.add(stack.pop());
               }

               if (!stack.empty() && stack.peek().getType() == 4) {
                  break;
               }

               throw new IllegalArgumentException("Misplaced function separator ',' or mismatched parentheses");
            default:
               throw new IllegalArgumentException("Unknown Token type encountered. This should not happen");
         }
      }

      while (!stack.empty()) {
         Token t = stack.pop();
         if (t.getType() == 5 || t.getType() == 4) {
            throw new IllegalArgumentException("Mismatched parentheses detected. Please check the expression");
         }

         output.add(t);
      }

      return output.toArray(new Token[0]);
   }
}
