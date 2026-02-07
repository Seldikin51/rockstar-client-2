package moscow.rockstar.ui.hud;

import java.util.Stack;

public class HudHistoryManager {
   private final Stack<HudHistoryManager.MoveAction> undoStack = new Stack<>();
   private final Stack<HudHistoryManager.MoveAction> redoStack = new Stack<>();

   public void registerMove(HudElement element, float fromX, float fromY, float toX, float toY) {
      this.undoStack.push(new HudHistoryManager.MoveAction(element, fromX, fromY, toX, toY));
      this.redoStack.clear();
   }

   public void undo() {
      if (!this.undoStack.isEmpty()) {
         HudHistoryManager.MoveAction lastAction = this.undoStack.pop();
         lastAction.element().pos(lastAction.fromX(), lastAction.fromY());
         this.redoStack.push(lastAction);
      }
   }

   public void redo() {
      if (!this.redoStack.isEmpty()) {
         HudHistoryManager.MoveAction redoAction = this.redoStack.pop();
         redoAction.element().pos(redoAction.toX(), redoAction.toY());
         this.undoStack.push(redoAction);
      }
   }

   private record MoveAction(HudElement element, float fromX, float fromY, float toX, float toY) {
   }
}
