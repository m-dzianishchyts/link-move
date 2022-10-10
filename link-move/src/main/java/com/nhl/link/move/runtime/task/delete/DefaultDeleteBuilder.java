package com.nhl.link.move.runtime.task.delete;

import com.nhl.link.move.DeleteBuilder;
import com.nhl.link.move.LmTask;
import com.nhl.link.move.annotation.AfterMissingTargetsFiltered;
import com.nhl.link.move.annotation.AfterSourceKeysExtracted;
import com.nhl.link.move.annotation.AfterTargetsMapped;
import com.nhl.link.move.mapper.Mapper;
import com.nhl.link.move.runtime.cayenne.ITargetCayenneService;
import com.nhl.link.move.runtime.task.BaseTaskBuilder;
import com.nhl.link.move.runtime.task.ITaskService;
import com.nhl.link.move.runtime.task.ListenersBuilder;
import com.nhl.link.move.runtime.task.MapperBuilder;
import com.nhl.link.move.runtime.token.ITokenManager;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.property.Property;

/**
 * @since 1.3
 */
public class DefaultDeleteBuilder<T extends DataObject> extends BaseTaskBuilder implements DeleteBuilder<T> {

    private ITaskService taskService;
    private ITokenManager tokenManager;
    private ITargetCayenneService targetCayenneService;
    private Class<T> type;

    private Expression targetFilter;
    private String extractorName;
    private Mapper mapper;
    private MapperBuilder mapperBuilder;
    private ListenersBuilder listenersBuilder;

    public DefaultDeleteBuilder(
            Class<T> type,
            ITargetCayenneService targetCayenneService,
            ITokenManager tokenManager,
            ITaskService taskService,
            MapperBuilder mapperBuilder) {

        this.tokenManager = tokenManager;
        this.taskService = taskService;
        this.targetCayenneService = targetCayenneService;
        this.type = type;

        this.mapperBuilder = mapperBuilder;
        this.listenersBuilder = createListenersBuilder();

        // always add stats listener..
        stageListener(DeleteStatsListener.instance());
    }

    ListenersBuilder createListenersBuilder() {
        return new ListenersBuilder(
                AfterTargetsMapped.class,
                AfterSourceKeysExtracted.class,
                AfterMissingTargetsFiltered.class);
    }

    @Override
    public DefaultDeleteBuilder<T> stageListener(Object listener) {
        listenersBuilder.addListener(listener);
        return this;
    }

    @Override
    public DefaultDeleteBuilder<T> batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public DefaultDeleteBuilder<T> targetFilter(Expression targetFilter) {
        this.targetFilter = targetFilter;
        return this;
    }

    @Override
    public DefaultDeleteBuilder<T> sourceMatchExtractor(String extractorName) {
        this.extractorName = extractorName;
        return this;
    }

    @Override
    public DefaultDeleteBuilder<T> matchBy(Mapper mapper) {
        this.mapper = mapper;
        return this;
    }

    @Override
    public DefaultDeleteBuilder<T> matchBy(String... keyAttributes) {
        this.mapper = null;
        this.mapperBuilder.matchBy(keyAttributes);
        return this;
    }

    @Override
    public DefaultDeleteBuilder<T> matchBy(Property<?>... matchAttributes) {
        this.mapper = null;
        this.mapperBuilder.matchBy(matchAttributes);
        return this;
    }

    @Override
    public DefaultDeleteBuilder<T> matchById() {
        this.mapper = null;
        this.mapperBuilder.matchById();
        return this;
    }

    @Override
    public DeleteTask<T> task() throws IllegalStateException {
        if (extractorName == null) {
            throw new IllegalStateException("Required 'sourceMatchExtractor' is not set");
        }

        return new DeleteTask<T>(extractorName, batchSize, type, targetFilter, targetCayenneService, tokenManager,
                createProcessor());
    }

    private DeleteSegmentProcessor<T> createProcessor() {
        Mapper mapper = this.mapper != null ? this.mapper : mapperBuilder.build();

        LmTask keysSubtask = taskService.extractSourceKeys(type).sourceExtractor(extractorName).matchBy(mapper).task();

        TargetMapper<T> targetMapper = new TargetMapper<>(mapper);
        ExtractSourceKeysStage sourceKeysExtractor = new ExtractSourceKeysStage(keysSubtask);
        MissingTargetsFilterStage<T> sourceMatcher = new MissingTargetsFilterStage<>();
        DeleteTargetStage<T> deleter = new DeleteTargetStage<>();

        return new DeleteSegmentProcessor<>(targetMapper, sourceKeysExtractor, sourceMatcher, deleter,
                listenersBuilder.getListeners());
    }
}
