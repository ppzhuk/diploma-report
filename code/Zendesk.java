public class Zendesk implements Closeable {
    private final AsyncHttpClient client;
    private final Logger logger;
	
    private Integer minRemainingApiCalls;
    private Integer curRemainingApiCalls = null;
    private Integer reqInterval;

    // ...

    // Данный метод отвечает за выполнление запроса,
    // полученного в качестве первого параметра.
    private <T> ListenableFuture<T> submit(Request request, ZendeskAsyncCompletionHandler<T> handler) {
        try {
          // Ожидаем 1 минуту, если количетсво зарпосов слишком велико.
            if (curRemainingApiCalls != null &&
                     curRemainingApiCalls < minRemainingApiCalls) {
                Thread.sleep(60 * 1000);
            } else {
                // Иначе ожидаем заданное время.
                if (reqInterval > 0) {
                    Thread.sleep(reqInterval);
                }
            }
        } catch (InterruptedException e) {
            throw new ZendeskException(e.getMessage(), e);
        }
        return client.executeRequest(request, handler);
    }
}
