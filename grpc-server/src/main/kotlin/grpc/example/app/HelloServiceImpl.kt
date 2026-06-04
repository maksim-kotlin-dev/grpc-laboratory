package grpc1.example.app

import grpc1.example.proto.HelloMessage
import grpc1.example.proto.HelloServiceGrpc
import grpc1.example.proto.StreamRequest
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.grpc.server.service.GrpcService
import java.time.LocalTime

@GrpcService
class HelloServiceImpl : HelloServiceGrpc.HelloServiceImplBase() {

    private val log = LoggerFactory.getLogger(HelloServiceImpl::class.java)

    override fun streamHello(
        request: StreamRequest,
        responseObserver: StreamObserver<HelloMessage>
    ) {
        log.info("New client connected — starting stream")
        val thread = Thread({
            try {
                while (!Thread.currentThread().isInterrupted) {
                    val now = LocalTime.now()
                    val text = "Хелло уорлд [%02d:%02d]".format(now.minute, now.second)
                    log.info("Sending: $text")
                    responseObserver.onNext(
                        HelloMessage.newBuilder().setText(text).build()
                    )
                    Thread.sleep(60_000)
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                log.info("Stream thread interrupted")
            } catch (e: Exception) {
                log.error("Stream error: ${e.message}")
                responseObserver.onError(e)
            }
        }, "hello-stream-thread")
        thread.isDaemon = true
        thread.start()
    }
}
