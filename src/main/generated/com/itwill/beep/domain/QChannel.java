package com.itwill.beep.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChannel is a Querydsl query type for ChannelEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChannel extends EntityPathBase<Channel> {

    private static final long serialVersionUID = 1445135381L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChannel channel = new QChannel("channel");

    public final QAccount account;

    public final NumberPath<Long> category_id = createNumber("category_id", Long.class);

    public final NumberPath<Long> channelId = createNumber("channelId", Long.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> created_time = createDateTime("created_time", java.time.LocalDateTime.class);

    public final StringPath profile_img = createString("profile_img");

    public final SetPath<Broadcast, EnumPath<Broadcast>> status = this.<Broadcast, EnumPath<Broadcast>>createSet("status", Broadcast.class, EnumPath.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final NumberPath<Long> viewers = createNumber("viewers", Long.class);

    public QChannel(String variable) {
        this(Channel.class, forVariable(variable), INITS);
    }

    public QChannel(Path<? extends Channel> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChannel(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChannel(PathMetadata metadata, PathInits inits) {
        this(Channel.class, metadata, inits);
    }

    public QChannel(Class<? extends Channel> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.account = inits.isInitialized("account") ? new QAccount(forProperty("account")) : null;
    }

}

