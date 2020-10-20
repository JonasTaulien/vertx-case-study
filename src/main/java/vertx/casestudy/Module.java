package vertx.casestudy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class Module extends AbstractModule {

    private final Vertx vertx;



    public Module(Vertx vertx) {
        this.vertx = vertx;
    }



    @Provides
    @Singleton
    Vertx provideVertx() {
        return this.vertx;
    }



    @Provides
    @Singleton
    PgPool providePgPool(Vertx vertx) {
        return PgPool.pool(
            vertx,
            new PgConnectOptions()
                .setHost("localhost")
                .setPort(5432)
                .setUser("example")
                .setPassword("example")
                .setDatabase("case-study"),
            new PoolOptions()
        );
    }



    @Provides
    @Singleton
    JWTAuth provideJwtAuth(Vertx vertx) {
        return JWTAuth.create(
            vertx,
            new JWTAuthOptions().addPubSecKey(
                new PubSecKeyOptions()
                    .setAlgorithm("RS256")
                    .setSecretKey(
                        "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDTxGPraZTmXAf0V5nKPR6Bb0MMWINGUr6aEZdzEWr2avJatxRcOT6ycEyRIF+r7/bVAY4pwAyk2bXu8MA/ZUU49ovRSdRYBtiHuuE/YXtBsteOJciy0A8sB/VlsSBNTyrP9emwCtlLA+K3/zq+Zd5u2anYmRWW9s/qGa6X7hCBbspBTflN/eI7YDA9/vJBUKveTMyUB+qavWtDyXeLPDeNcDkOOObgRPInrf/nGyOPQ/p3Je4BokGpup8wLJZng+mOFDnTCAnW7BXhy/UcEsMim3jy2WalmRGYc08SPiH9Hwq5RfU1jelM9zVdsyG3+dsWsxYPbp+A7xEjvKDI1y6TAgMBAAECggEAJRd2BOrWWM7v2b1qQ4EXEcbCMKxkOfIOToAcTdxUJtsunfMJTun78Fc5IgZQm5YJXt/J3cg0rr8vXa2vjqsXR9XvQXY0CkMQtB1ojKhvZV+E3/IASEmnDqhKNEilBsWCEzDYgKw1ySziDiZXTLgdEL9xP2342Rtb6X4cFaJuI61JgkYlh5iSEMS2+OPdJN0AbenbuzT4VOAAKUStMetHUktYvmmRmtkqyrZ6pKoKGDxOeVC0GTroo95YQ6f4EqWYlHZ2OIJ/cO/a3BDEBgQuuIxkWqhsa33uxMd2o3mDIm+qzt5jvWdbcHhFjOeF/17ETnQV9OYR3QKQ6kIhcAcCGQKBgQDxzSju3IvnIhxxKrZXk8i0cPhb/9qNl4re8+jsEd2TKXtkkJ5GLuokaCa7VuKBtRuaTX5g4u0mCfUSbCtK2i0RsFTiYa/jnS0l5CWhdnOu3pxRXI8zkBxXgLVm47R5p1qo6BaFTxBWbXxPT5l2PVW0ethXiKPQcvJhJAaW63NhrQKBgQDgM77Qjc+Fc0Gn4/aZuZfRANk1hdJWqPUyEuLJdZFfdzt7C1iR63pNNFaNnsTZBSslw2XRuJxPucDdEtu5FXQH7MFmfzjiMi3TVcWMB75rNAa3kKcKErx7OyEYWsfdjDtmd1meP2eh6HlcARjHq7ALdn2U0qliT27DBqsM9hFZPwKBgQDurDR4Ocf6eGgcqC1eGqiku1Ha9ZwT4P6ZfaZuxl+wr+R1am5gFlTVZNy+VE+1+j9puo5BMh0lS/TUn2LIOrz71dKjtgr4oB99ZEpkqtgbRu6DPNq46cmf8ZKdIav13OQXDyeTpGwa820dmBOBa59aS1uaY2xXB+8JRZcfhv7X7QKBgQDaKuBKl0RSCubf1d/YlRYbZmT3RUJ5263FzUTsX0BYEnFJ+a91Rxxa1kxHQTEyMWpcM/j3jFpm5k27kqdHQo4HTCaic7hJvCSFmthBvCNatGS46cTx82t/yvkYdvY4K0Z+HjOR3wccMVku405WtkX30ZTAZgI3zS3uD6YRVb1qBwKBgQDxtm3Pko4vSwenjoHik2reBgROXaU3UjWm4iFJJeQNicOp2wmtJJfMDQtZjdOs6gd4JZvAFNbqTlfLsgvPknlvRCQQh3HXn7Xcb4mSIz8YYUiTNIlUS4JWOpTXcF5YKk9aN2TRzNVMSi/3rL+X7w0MC8cy0VgruB8QKP7AZa18Dg==")
                    .setPublicKey(
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA08Rj62mU5lwH9FeZyj0egW9DDFiDRlK"
                            + "+mhGXcxFq9mryWrcUXDk+snBMkSBfq"
                            + "+/21QGOKcAMpNm17vDAP2VFOPaL0UnUWAbYh7rhP2F7QbLXjiXIstAPLAf1ZbEgTU8qz/XpsArZSwPit"
                            + "/86vmXebtmp2JkVlvbP6hmul+4QgW7KQU35Tf3iO2AwPf7yQVCr3kzMlAfqmr1rQ8l3izw3jXA5Djjm4ETyJ63"
                            + "/5xsjj0P6dyXuAaJBqbqfMCyWZ4PpjhQ50wgJ1uwV4cv1HBLDIpt48tlmpZkRmHNPEj4h"
                            + "/R8KuUX1NY3pTPc1XbMht/nbFrMWD26fgO8RI7ygyNcukwIDAQAB")
            )
        );
    }



    @Provides
    @Singleton
    JWTAuthHandler provideJwtAuthHandler(JWTAuth jwtAuth) {
        return JWTAuthHandler.create(jwtAuth);
    }
}
