package com.github..idea.gitlab;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.common.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github..idea.gitlab.HttpUtils.assertHasBody;

public class JsonHttpResponseCallback<T> implements Callback {
    protected final Logger log = Logger.getInstance("#" + JsonHttpResponseCallback.class.getName());

    private Type typeToken;

    private final CompletableFuture<T> result;

    private final Gson gson;

    private JsonHttpResponseCallback(CompletableFuture<T> result, Gson gson) {
        this.result = result;
        this.gson = gson;
    }

    public JsonHttpResponseCallback(Class<T> resultClass, CompletableFuture<T> result, Gson gson) {
        this.typeToken = new TypeToken<T>(){}.getType();
        this.result = result;
        this.gson = gson;
    }

    public static <E> JsonHttpResponseCallback ofList(CompletableFuture<List<E>> result, Gson gson) {
        JsonHttpResponseCallback callback = new JsonHttpResponseCallback(result, gson);
        callback.typeToken = new TypeToken<List<E>>(){}.getType();

        return callback;
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        try(ResponseBody body = response.body()) {
            String json = assertHasBody(response, body).string();
            onRawResponseBody(response, json);
            Type typeToken = new TypeToken<T>(){}.getType();
            T deserializedJson = this.gson.fromJson(json, typeToken);
            result.complete(handleResponse(response, body, json, deserializedJson));
        } catch (JsonSyntaxException e) {
            result.completeExceptionally(e);
        }
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        if (e instanceof SocketTimeoutException) {
            result.completeExceptionally(new GitLabIOException("GitLab network connectivity failed: " + e.getMessage(), e));
            return;
        }
        result.completeExceptionally(e);
    }

    protected void onRawResponseBody(Response response, String rawResponseBodyString) {
        logRawResponseBody(response, rawResponseBodyString);
    }

    protected void logRawResponseBody(Response response, String rawResponseBodyString) {
        log.debug("HTTP " + response.code() + "\n" + rawResponseBodyString);
    }

    protected void logAndConsumeRawResponseBody(Response response) {
        try(ResponseBody body = response.body()) {
            String bodyPayload = assertHasBody(response, body).string();
            logRawResponseBody(response, bodyPayload);
        } catch (IOException e) {
            log.debug("Cannot log and consume response body", e);
        }
    }

    protected void completeExceptionally(CompletableFuture<T> result, Throwable throwable, Response response) {
        logAndConsumeRawResponseBody(response);
        result.completeExceptionally(throwable);
    }
    protected T handleResponse(Response response, ResponseBody body, String json, T object) {
        return object;
    }

    private boolean assertNotNullBody(Response response) {
        ResponseBody body = response.body();
        if (body == null) {
            result.completeExceptionally(GitLabHttpResponseException.ofNullResponse(response));
            return false;
        }
        return true;
    }
}
