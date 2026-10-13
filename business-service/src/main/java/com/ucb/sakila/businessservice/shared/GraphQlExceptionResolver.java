package com.ucb.sakila.businessservice.shared;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

// Traduce cada BusinessServiceException a un GraphQLError con extensions: {code, statusCode}.
@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (!(ex instanceof BusinessServiceException bse)) {
            return null; // deja que el resolver por defecto lo convierta en un 500 genérico
        }
        return GraphqlErrorBuilder.newError(env)
                .message(bse.getMessage())
                .errorType(errorTypeFor(bse.getStatusCode()))
                .extensions(Map.of("code", bse.getCode(), "statusCode", bse.getStatusCode()))
                .build();
    }

    private ErrorType errorTypeFor(int statusCode) {
        return switch (statusCode) {
            case 400 -> ErrorType.BAD_REQUEST;
            case 401 -> ErrorType.UNAUTHORIZED;
            case 403 -> ErrorType.FORBIDDEN;
            case 404 -> ErrorType.NOT_FOUND;
            default -> ErrorType.INTERNAL_ERROR;
        };
    }
}
