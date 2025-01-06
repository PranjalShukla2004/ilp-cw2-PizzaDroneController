package inf.ed.cw_ilp.api;
import inf.ed.cw_ilp.model.pathFinder.nameData;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
public class DynamicDataService {

    private final WebClient webClient;

    public DynamicDataService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://ilp-rest-2024.azurewebsites.net/").build();
    }
    // no-fly zone
    public List<nameData.NamedRegion> fetchNoFlyZones() {
        return webClient.get()
                .uri("/noflyzones")
                .retrieve()
                .bodyToFlux(nameData.NamedRegion.class)
                .collectList()
                .block();
    }
    // central area coords
    public nameData.NamedRegion fetchCentralArea() {
        return webClient.get()
                .uri("/centralArea")
                .retrieve()
                .bodyToMono(nameData.NamedRegion.class)
                .block();
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
