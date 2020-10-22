package vertx.casestudy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.client.WebClient;
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
    PgPool providePgClient() {
        return PgPool.pool(
            this.vertx,
            new PgConnectOptions()
                .setHost("localhost")
                .setPort(5432)
                .setDatabase("case-study")
                .setPassword("example")
                .setUser("example"),
            new PoolOptions().setMaxSize(10)
        );
    }



    @Provides
    @Singleton
    JWTAuth provideJWTAuth() {
        return JWTAuth.create(
            this.vertx,
            new JWTAuthOptions()
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setAlgorithm("RS256")
                        .setSecretKey(
                            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDFow0OtZUGuwVOpupGBFYRdqpp4TZm4o7Al2KPB6X4xn5zddGl7m6voWC0DgwI01M64xuSrjlVT2HloKdUNsm71Cd3TDLTEmwdplgh1gHdgECrcCsL1DovtKYkevLRXeNF7YJK4KqWGy4UGWcSoQhPGerru8LsaoRQfZckilDOzrTg3N2RRM5iQ7d+LfpHw07OqRa/9bO4S3moH2eB75eo+u9I8axRwdsVWPb9+zicBwZ1ilshRnqcRtkcU/MqWdb0ZbsTzqdL2+TbU8Ls0aJYwYcMHlUqx3fgaxWeyfyImT6flP5umNj113MYOirXVRMV9j82KRRkptlx61mI8G7zAgMBAAECggEAfWft1CSSUHgBcRvm0dDUkDY9Afw+2d/udvRYPKu8u/OvrzHWerSllVoQE5BKQJtfV6FeEJ/uj1xUoTFkm8I8FawT8tXt9hLdeZ7gk2/JNTS5VE9TcSkUVssJFWLSitlJakjAASU3+RF8FpLb3W1C6XZno3c7w2n/VVervSrrBZ3znd9pZLHyzLfqgBv9GsLPV7bE5PiNOJTZTmVS3tq3lULtcMKHDWjkNW9ndfV7zRPCUEn/p4fVmm5nl0Ub2aukh2Dmau5FkIHR3FXd+zgo5hGxuOZbX/hgeHKIjRMvzI8Tep2AiknhVtO2gqFu6QyIG2LeupAZIwX17XxXqE7tYQKBgQD0soFuNTPJfUzMIXRgeFkA1R+6CVgt1S05EFCbPZOp8DE1MJ7aVVQpRe9MBuKlfA0nunAWiMjXBQ8/QE6uDobXZ6VzU9w14Hl0VBH+c8GcJE3bfqrhkd07ZIkz3K1DQ6P83gJBgoMs/tIWzyWA0PlOxXX3A7iQ4FFWfK3k8c98sQKBgQDOxBD88Tz4awh4DxZ9Bqd25zi57IcD5qnkG+HfuapcP3rFZLCQ5GbWzMx/ef4l7/4CuzL9tiKoX1BcTpSmb/ymIgCU5g4Uqtnod01TnPt8o9ktxKVR9WSXbbYIP5Y1UmKbWObXWGd50DG+X8W3JxsjtywPZ5eQQl1ycEFLwmI+4wKBgQCtMF59Uzh5Jq8hV5hX4zYTacTP1mdL4TLlzY9PoPCPecpKPERigbxUjgQFimYF/FwPP1ywBlYoIZOeD/TgKrXzZY9rpfIZS+yKkio+L03cJrHYZbmqd6PespSNWWZkUk6R3cw485fGVAD8VdbUapPZ1dZdY4vRNoM7NHmbri5NsQKBgClz73/2kWWwd7MyOyz3r41MD/hebjcfAIKGAIPNAlWaOG/onul9kQyRZJJOD+DlO4SqqS9qW4psCX1oF69ClmFsQbeYY8xK8IMFoBAaUeUMKFdfrFFBJFijmzIOJXwtNy2z5hvYUm5+ieqD4uy0P2GqsH+qIh2+4GM4FC8vGmlHAoGAB44BaVMsIof49k3J3QMbZDmg71W2MEJX7m+LsWTxCJWHO4nwG8KO8IEIMzajiWIPwcp7+/efJRqW7Az6JSAEAEi2b01/+nd5eAwQz8KZFY3QID2d67jDGdISgG6sayHlKPd8a0VMn8Q0SpextqRQK5rkR6H4v45q/7ivCTnJJnM=")
                        .setPublicKey(
                            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxaMNDrWVBrsFTqbqRgRWEXaqaeE2ZuKOwJdijwel+MZ"
                                +
                                "+c3XRpe5ur6FgtA4MCNNTOuMbkq45VU9h5aCnVDbJu9Qnd0wy0xJsHaZYIdYB3YBAq3ArC9Q6L7SmJHry0V3jRe2CSuCqlhsuFBlnEqEITxnq67vC7GqEUH2XJIpQzs604NzdkUTOYkO3fi36R8NOzqkWv/WzuEt5qB9nge+XqPrvSPGsUcHbFVj2/fs4nAcGdYpbIUZ6nEbZHFPzKlnW9GW7E86nS9vk21PC7NGiWMGHDB5VKsd34GsVnsn8iJk+n5T+bpjY9ddzGDoq11UTFfY/NikUZKbZcetZiPBu8wIDAQAB")
                )
        );
    }



    @Provides
    @Singleton
    JWTAuthHandler provideJWTAuthHandler(JWTAuth jwtAuth) {
        return JWTAuthHandler.create(jwtAuth);
    }



    @Provides
    @Singleton
    ConfigRetriever provideConfigRetriever() {
        return ConfigRetriever.create(
            this.vertx,
            new ConfigRetrieverOptions()
                .addStore(
                    new ConfigStoreOptions()
                        .setType("file")
                        .setConfig(new JsonObject().put("path", "config.json"))
                )
        );
    }
}
