package com.test.slideshow.tasks;

/**
 * Created by Nikita on 20.10.2014.
 */
public interface AsyncListener<Result> {
    void onPostExecute(Result result, Exception exception);
}