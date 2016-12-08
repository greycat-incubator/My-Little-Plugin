package lu.jimenez.research.mylittleplugin;

import org.mwg.*;
import org.mwg.base.BaseNode;
import org.mwg.plugin.Job;
import org.mwg.struct.RelationIndexed;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

public class ActionStoreGetInVar implements Action {

    private final String _name;
    private final String _variable;
    private final String[] _params;

    ActionStoreGetInVar(final String p_name, final String p_variable, final String... p_params) {
        super();
        this._name = p_name;
        this._variable = p_variable;
        this._params = p_params;
    }

    public void eval(final TaskContext taskContext) {
        final TaskResult finalResult = taskContext.newResult();
        final String flatName = taskContext.template(_name);
        final TaskResult previousResult = taskContext.result();
        if (previousResult != null) {
            final int previousSize = previousResult.size();
            final DeferCounter defer = taskContext.graph().newCounter(previousSize);
            for (int i = 0; i < previousSize; i++) {
                final Object loop = previousResult.get(i);
                if (loop instanceof BaseNode) {
                    final Node casted = (Node) loop;

                    switch (casted.type(flatName)) {
                        case Type.RELATION_INDEXED:
                            if (_params != null && _params.length > 0) {
                                RelationIndexed relationIndexed = (RelationIndexed) casted.get(flatName);
                                if (relationIndexed != null) {
                                    Query query = taskContext.graph().newQuery();
                                    String previous = null;
                                    for (int k = 0; k < _params.length; k++) {
                                        if (previous != null) {
                                            query.add(previous, _params[k]);
                                            previous = null;
                                        } else {
                                            previous = _params[k];
                                        }
                                    }
                                    relationIndexed.findByQuery(query, new Callback<Node[]>() {

                                        public void on(Node[] result) {
                                            if (result != null) {
                                                for (int j = 0; j < result.length; j++) {
                                                    if (result[j] != null) {
                                                        finalResult.add(result[j]);
                                                    }
                                                }
                                            }
                                            defer.count();
                                        }
                                    });
                                } else {
                                    defer.count();
                                }
                            }
                        case Type.RELATION:
                            if (_params == null || _params.length == 0) {
                                casted.relation(flatName, new Callback<Node[]>() {

                                    public void on(Node[] result) {
                                        if (result != null) {
                                            for (int j = 0; j < result.length; j++) {
                                                finalResult.add(result[j]);
                                            }
                                        }
                                        defer.count();
                                    }
                                });
                            }
                            break;
                        default:
                            Object resolved = casted.get(flatName);
                            if (resolved != null) {
                                finalResult.add(resolved);
                            }
                            defer.count();
                            break;

                    }
                } else {
                    //TODO add closable management
                    finalResult.add(loop);
                    defer.count();
                }
            }
            defer.then(new Job() {
                public void run() {
                    taskContext.defineVariable(taskContext.template(_variable), finalResult);
                    taskContext.continueTask();
                }
            });
        } else {
            taskContext.continueTask();
        }
    }
    @Override
    public String toString() {
        return "StoreGetAsVar " + _name + " " + _variable;
    }
}
