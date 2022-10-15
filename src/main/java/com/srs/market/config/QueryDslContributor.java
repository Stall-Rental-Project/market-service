package com.srs.market.config;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;


public class QueryDslContributor implements MetadataBuilderContributor {
    @Override
    public void contribute(MetadataBuilder metadata) {
        metadata.applySqlFunction("STRING_AGG", new StandardSQLFunction("STRING_AGG", StandardBasicTypes.STRING));
    }
}
