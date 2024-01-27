package ru.clevertec.ecl.knyazev.service.integration;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import ru.clevertec.ecl.knyazev.cache.operator.AbstractCacheOperator;
import ru.clevertec.ecl.knyazev.config.TestContainerInitializer;
import ru.clevertec.ecl.knyazev.data.http.house.request.PostPutHouseRequestDTO;
import ru.clevertec.ecl.knyazev.data.http.person.request.PostPutPersonRequestDTO;
import ru.clevertec.ecl.knyazev.entity.House;
import ru.clevertec.ecl.knyazev.entity.Person;
import ru.clevertec.ecl.knyazev.service.HouseService;
import ru.clevertec.ecl.knyazev.service.PersonService;
import ru.clevertec.ecl.knyazev.util.integration.HouseIntegrationTestData;
import ru.clevertec.ecl.knyazev.util.integration.PersonIntegrationTestData;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
public class HouseServiceImplTest extends TestContainerInitializer {

    @Autowired
    private HouseService houseServiceImpl;

    @SpyBean
    private AbstractCacheOperator<UUID, House> houseCacheOperatorSpy;

    @Test
    public void checkHouseServiceOperationsWithEnabledCache() throws InterruptedException {
        UUID getHouseUUID = HouseIntegrationTestData.houseGettingUUID();
        PostPutHouseRequestDTO houseSavingRequestDTO = HouseIntegrationTestData.houseSavingRequest();
        PostPutHouseRequestDTO houseUpdatingRequestDTO = HouseIntegrationTestData.houseUpdatingRequest();
        UUID deletingHouseUUID = HouseIntegrationTestData.houseDeletingUUID();

        ExecutorService executorService = Executors.newFixedThreadPool(6);

        executorService.submit(() -> houseServiceImpl.getHouseResponseDTO(getHouseUUID));
        executorService.submit(() -> houseServiceImpl.add(houseSavingRequestDTO));
        executorService.submit(() -> houseServiceImpl.update(houseUpdatingRequestDTO));
        executorService.submit(() -> houseServiceImpl.remove(deletingHouseUUID));

        executorService.awaitTermination(4L, TimeUnit.SECONDS);
        executorService.shutdown();

        Mockito.verify(houseCacheOperatorSpy,Mockito.times(2))
                .find(Mockito.any(UUID.class));
        Mockito.verify(houseCacheOperatorSpy, Mockito.times(4))
                .add(Mockito.any(UUID.class), Mockito.any(House.class));
        Mockito.verify(houseCacheOperatorSpy, Mockito.never())
                .delete(Mockito.any(UUID.class));
    }
}