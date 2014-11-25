import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class DiscoverAndTakeChallengeSimulation extends Simulation {

  val baseUrl = System.getProperty("base.url", "http://localhost:8080")

  val httpProtocol = http
    .baseURL(baseUrl)
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-gb,en;q=0.5")
    .connection("keep-alive")

  val scn = scenario("Taking Clean Code challenge")
    .exec(http("discover").get("/tracks")
    .queryParam("page", "0")
    .queryParam("size", "10")
    .check(status.is(200))
    )

    .exec(http("apply").post("/challenges/CLNCDE/0")
    .check(status.is(200))
    .check(jsonPath("$.links[0].href").saveAs("response-link")))

    .repeat(5, "counter") {
    exec(http("respond  to question ${counter}").post("${response-link}")
      .check(status.is(200))
      .check(jsonPath("$.links[0].href").saveAs("response-link")))
      .pause(1 seconds)
  }

    .exec(http("respond to the last question ").post("${response-link}")
    .check(status.is(200))
    .check(jsonPath("$['deck.accomplishmentMessage']").exists))


  setUp(scn.inject(
    rampUsersPerSec(1) to 150 during (10 seconds)
  ).protocols(httpProtocol))
}
