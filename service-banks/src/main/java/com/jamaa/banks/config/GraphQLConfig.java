package com.jamaa.banks.config;

import graphql.language.FloatValue;
import graphql.language.IntValue;
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
                .scalar(dateTimeScalar())
                .scalar(doubleScalar());
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
                            return LocalDateTime.parse(((StringValue) input).getValue(), DateTimeFormatter.ISO_DATE_TIME);
                        }
                        throw new IllegalArgumentException("Expected a StringValue AST node.");
                    }
                })
                .build();
    }

    // Scalar pour gérer Double (float/double) <-> Double
    private GraphQLScalarType doubleScalar() {
        return GraphQLScalarType.newScalar()
                .name("Double")
                .description("Custom scalar for java.lang.Double")
                .coercing(new Coercing<Double, Double>() {
                    @Override
                    public Double serialize(Object dataFetcherResult) {
                        if (dataFetcherResult instanceof Number) {
                            return ((Number) dataFetcherResult).doubleValue();
                        }
                        throw new IllegalArgumentException("Expected a Number for serialization.");
                    }

                    @Override
                    public Double parseValue(Object input) {
                        if (input instanceof Number) {
                            return ((Number) input).doubleValue();
                        }
                        throw new IllegalArgumentException("Expected a Number to parse into Double.");
                    }

                    @Override
                    public Double parseLiteral(Object input) {
                        if (input instanceof FloatValue) {
                            return ((FloatValue) input).getValue().doubleValue();
                        } else if (input instanceof IntValue) {
                            return ((IntValue) input).getValue().doubleValue();
                        }
                        throw new IllegalArgumentException("Expected a FloatValue or IntValue.");
                    }
                })
                .build();
    }
}
