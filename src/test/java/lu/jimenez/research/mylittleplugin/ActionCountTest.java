package lu.jimenez.research.mylittleplugin;

import org.junit.jupiter.api.Test;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static lu.jimenez.research.mylittleplugin.MyLittleActions.count;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mwg.core.task.Actions.newTask;
import static org.mwg.core.task.Actions.readGlobalIndex;

class ActionCountTest extends ActionTest {

    @Test
    public void testEmpty() {
        initGraph();
        newTask()
                .then(count())
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext context) {
                        assertEquals(context.result().get(0), 0);
                    }
                })
                .execute(graph, null);
        removeGraph();
    }

    @Test
    public void testComplex() {
        initGraph();
        newTask()
                .then(readGlobalIndex("nodes"))
                .then(count())
                .thenDo(new ActionFunction() {
                    public void eval(TaskContext context) {
                        assertEquals(context.result().get(0), 3);
                    }
                })
                .execute(graph, null);
        removeGraph();
    }


}