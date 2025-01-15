package inf.ed.cw_ilp.api;
import inf.ed.cw_ilp.controller.PizzaDroneController;
import inf.ed.cw_ilp.model.pathFinder.nameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
public class DynamicDataService {

    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(DynamicDataService.class);

    public DynamicDataService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://ilp-rest-2024.azurewebsites.net/").build();
    }
    // no-fly zone
    public List<nameData.NamedRegion> fetchNoFlyZones() {
        List<nameData.NamedRegion> noFlyZones = webClient.get()
                .uri("/noflyzones")
                .retrieve()
                .bodyToFlux(nameData.NamedRegion.class)
                .collectList()
                .block();
        return noFlyZones;
    }
    // central area coords
    public nameData.NamedRegion fetchCentralArea() {
        nameData.NamedRegion centralArea =  webClient.get()
                .uri("/centralArea")
                .retrieve()
                .bodyToMono(nameData.NamedRegion.class)
                .block();
        log.info("Fetched Central Area: {}", centralArea);
        return centralArea;
    }
    // list of different restaurants
    public List<nameData.Restaurant> fetchRestaurants() {
        return webClient.get()
                .uri("/restaurants")
                .retrieve()
                .bodyToFlux(nameData.Restaurant.class)
                .collectList()
                .block();
    }
}
