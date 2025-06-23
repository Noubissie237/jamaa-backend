package com.jamaa_bank.service_recharge_retrait.config;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(dateTimeScalar());
    }

    // Scalar pour gérer LocalDateTime <-> String
    private GraphQLScalarType dateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("Custom scalar for java.time.LocalDateTime")
                .coercing(new Coercing<LocalDateTime, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult instanceof LocalDateTime) {
                            return ((LocalDateTime) dataFetcherResult).format(DateTimeFormatter.ISO_DATE_TIME);
                        }
                        throw new IllegalArgumentException("Expected a LocalDateTime object.");
                    }

                    @Override
                    public LocalDateTime parseValue(Object input) {
                        if (input instanceof String) {
                            return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_DATE_TIME);
                        }
                        throw new IllegalArgumentException("Expected a String to parse into LocalDateTime.");
                    }

                    @Override
                    public LocalDateTime parseLiteral(Object input) {
                        if (input instanceof StringValue) {
                            return LocalDateTime.parse(((StringValue) input).getValue(),
                                    DateTimeFormatter.ISO_DATE_TIME);
                        }
                        throw new IllegalArgumentException("Expected a StringValue AST node.");
                    }
                })
                .build();
    }
}
