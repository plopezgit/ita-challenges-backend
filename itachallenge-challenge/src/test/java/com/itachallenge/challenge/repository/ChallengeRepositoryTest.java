package com.itachallenge.challenge.repository;

import com.itachallenge.challenge.document.*;
import com.itachallenge.challenge.document.Locale;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static com.itachallenge.challenge.document.Locale.EN;
import static com.itachallenge.challenge.document.Locale.ES;
import static org.junit.Assert.*;
import static org.springframework.test.util.AssertionErrors.fail;

@DataMongoTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ChallengeRepositoryTest {

    @Container
    static MongoDBContainer container = new MongoDBContainer("mongo")
            .withStartupTimeout(Duration.ofSeconds(60));

    @DynamicPropertySource
    static void initMongoProperties(DynamicPropertyRegistry registry) {
        System.out.println("container url: {}" + container.getReplicaSetUrl("challenges"));
        System.out.println("container host/port: {}/{}" + container.getHost() + " - " + container.getFirstMappedPort());

        registry.add("spring.data.mongodb.uri", () -> container.getReplicaSetUrl("challenges"));
    }

    @Autowired
    private ChallengeRepository challengeRepository;

    UUID uuid_1 = UUID.fromString("8ecbfe54-fec8-11ed-be56-0242ac120002");
    UUID uuid_2 = UUID.fromString("26977eee-89f8-11ec-a8a3-0242ac120003");
    UUID uuid_3 = UUID.fromString("2f948de0-6f0c-4089-90b9-7f70a0812319");

    @BeforeEach
    public void setUp() {

        //challengeRepository.deleteAll().block();

        Set<UUID> UUIDSet = new HashSet<>(Arrays.asList(uuid_2, uuid_1));
        Set<UUID> UUIDSet2 = new HashSet<>(Arrays.asList(uuid_2, uuid_1));

        ExampleDocument example = new ExampleDocument(uuid_1, "Example Text 1", EN);
        ExampleDocument example2 = new ExampleDocument(uuid_2, "Texto ejemplo 2", ES);
        List<ExampleDocument> exampleList = new ArrayList<ExampleDocument>(Arrays.asList(example2, example));

        UUID uuidLang1 = UUID.fromString("09fabe32-7362-4bfb-ac05-b7bf854c6e0f");
        UUID uuidLang2 = UUID.fromString("409c9fe8-74de-4db3-81a1-a55280cf92ef");
        UUID[] idsLanguages = new UUID[]{uuidLang1, uuidLang2};
        String[] languageNames = new String[]{"name1", "name2"};
        LanguageDocument language1 = new LanguageDocument(idsLanguages[0], languageNames[0]);
        LanguageDocument language2 = new LanguageDocument(idsLanguages[1], languageNames[1]);
        Set<LanguageDocument> languageSet = Set.of(language1, language2);
        Set<LanguageDocument> languageSet3 = Set.of(language1);

        List<UUID> solutionList = List.of(UUID.randomUUID(),UUID.randomUUID());

        DetailDocument detail = new DetailDocument("Description", exampleList, "Detail note", EN);

        ChallengeDocument challenge = new ChallengeDocument
                (uuid_1, "Loops", "MEDIUM", LocalDateTime.now(), detail, languageSet, solutionList, UUIDSet, UUIDSet2, EN);
        ChallengeDocument challenge2 = new ChallengeDocument
                (uuid_2, "Switch", "EASY", LocalDateTime.now(), detail, languageSet, solutionList, UUIDSet, UUIDSet2, EN);
        ChallengeDocument challenge3 = new ChallengeDocument
                (uuid_3, "If", "HARD", LocalDateTime.now(), detail, languageSet3, solutionList, UUIDSet, UUIDSet2, EN);

        challengeRepository.saveAll(Flux.just(challenge, challenge2, challenge3)).blockLast();
    }

    @DisplayName("Repository not null Test")
    @Test
    void testDB() {
        assertNotNull(challengeRepository);
    }

    @DisplayName("Find Challenges in Locale for a Page")
    @Test
    void findAllLocaleTest(Locale locale) {

        Flux<ChallengeDocument> challengesOffset0Limit1 = challengeRepository.findAllByUuidNotNullAndLocale(locale).skip(0).take(1);
        StepVerifier.create(challengesOffset0Limit1)
                .expectNextCount(1)
                .verifyComplete();

        Flux<ChallengeDocument> challengesOffset0Limit2 = challengeRepository.findAllByUuidNotNullAndLocale(locale).skip(0).take(2);
        StepVerifier.create(challengesOffset0Limit2)
                .expectNextCount(2)
                .verifyComplete();

        Flux<ChallengeDocument> challengesOffset1Limit1 = challengeRepository.findAllByUuidNotNullAndLocale(locale).skip(1).take(1);
        StepVerifier.create(challengesOffset1Limit1)
                .expectNextCount(1)
                .verifyComplete();

        Flux<ChallengeDocument> challengesOffset1Limit2 = challengeRepository.findAllByUuidNotNullAndLocale(locale).skip(2).take(2);
        StepVerifier.create(challengesOffset1Limit2)
                .expectNextCount(1)
                .verifyComplete();
    }

    @DisplayName("Exists by UUID and Locale Test")
    @Test
    void existsByUuidAndLocaleTest(Locale locale) {
        Boolean exists = challengeRepository.existsByUuidAndLocale(uuid_1, locale).block();
        assertEquals(true, exists);
    }

    @DisplayName("Find by UUID Test")
    @Test
    void findByUuidAndLocaleTest(Locale locale) {

        Mono<ChallengeDocument> firstChallenge = challengeRepository.findByUuidAndLocale(uuid_1, locale);
        firstChallenge.blockOptional().ifPresentOrElse(
                u -> assertEquals(u.getUuid(), uuid_1),
                () -> fail("Challenge not found: " + uuid_1));

        Mono<ChallengeDocument> secondChallenge = challengeRepository.findByUuidAndLocale(uuid_2, locale);
        secondChallenge.blockOptional().ifPresentOrElse(
                u -> assertEquals(u.getUuid(), uuid_2),
                () -> fail("Challenge not found: " + uuid_2));
    }

    @DisplayName("Delete by UUID and Locale Test")
    @Test
    void deleteByUuidAndLocaleTest(Locale locale) {

        Mono<ChallengeDocument> firstChallenge = challengeRepository.findByUuidAndLocale(uuid_1, locale);
        firstChallenge.blockOptional().ifPresentOrElse(
                u -> {
                    Mono<Void> deletion = challengeRepository.deleteByUuidAndLocale(uuid_1, locale);
                    StepVerifier.create(deletion)
                            .expectComplete()
                            .verify();
                },
                () -> fail("Challenge to delete not found: " + uuid_1 + " in " + locale)
        );

        Mono<ChallengeDocument> secondChallenge = challengeRepository.findByUuidAndLocale(uuid_2, locale);
        secondChallenge.blockOptional().ifPresentOrElse(
                u -> {
                    Mono<Void> deletion = challengeRepository.deleteByUuidAndLocale(uuid_2, locale);
                    StepVerifier.create(deletion)
                            .expectComplete()
                            .verify();
                },
                () -> fail("Challenge to delete not found: " + uuid_2 + " in " + locale)
        );
    }


    @DisplayName("Find by Title Test")
    @Test
    void findByChallengeTitleTest() {

        Mono<ChallengeDocument> firstChallenge = challengeRepository.findByTitle("Loops");
        firstChallenge.blockOptional().ifPresentOrElse(
                u -> assertEquals(u.getTitle(), "Loops"),
                () -> fail("Challenge with name Loops  not found."));

        Mono<ChallengeDocument> secondChallenge = challengeRepository.findByTitle("If");
        secondChallenge.blockOptional().ifPresentOrElse(
                u -> assertEquals(u.getTitle(), "If"),
                () -> fail("Challenge with name If not found."));
    }

    @DisplayName("Find by Level, LanguagesId and Locale - Get one Test")
    @Test
    void findAllChallengeByLanguagesAndLevelAndLocaleGetOne(Locale locale) {
        // Arrange
        UUID uuidLang1 = UUID.fromString("09fabe32-7362-4bfb-ac05-b7bf854c6e0f");

        Flux<ChallengeDocument> filteredChallenges1 = challengeRepository
                .findByLevelAndLanguages_IdLanguageAndLocale("MEDIUM", uuidLang1, locale);
        Flux<ChallengeDocument> filteredChallenges2 = challengeRepository
                .findByLevelAndLanguages_IdLanguageAndLocale("EASY", uuidLang1, locale);

        StepVerifier.create(filteredChallenges1)
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(filteredChallenges2)
                .expectNextCount(1)
                .verifyComplete();
    }

    @DisplayName("Find by idLanguage Test")
    @Test
    void findByLanguages_idLanguageAndLocale_test(Locale locale) {
        // Arrange
        UUID uuidLang1 = UUID.fromString("409c9fe8-74de-4db3-81a1-a55280cf92ef");
        UUID uuidLang2 = UUID.fromString("09fabe32-7362-4bfb-ac05-b7bf854c6e0f");

        Flux<ChallengeDocument> challengeFiltered1 = challengeRepository
                .findByLanguages_IdLanguageAndLocale(uuidLang1, locale);
        Flux<ChallengeDocument> challengeFiltered2 = challengeRepository
                .findByLanguages_IdLanguageAndLocale(uuidLang2, locale);

        StepVerifier.create(challengeFiltered1)
                .expectNextCount(2)
                .verifyComplete();
        StepVerifier.create(challengeFiltered2)
                .expectNextCount(3)
                .verifyComplete();
    }

    @DisplayName("Find by LanguageName and Locale Test")
    @Test
    void findByLanguages_LanguageNameAndLocale_test(Locale locale) {
        Flux<ChallengeDocument> findByNameClass = challengeRepository
                .findByLanguages_LanguageNameAndLocale("name1", locale);

        StepVerifier.create(findByNameClass)
                .expectNextCount(3)
                .verifyComplete();
    }

    @DisplayName("Find by Level and Locale Flux Test")
    @Test
    void findByLevelAndLocaleFlux_test(Locale locale) {
        Flux<ChallengeDocument> filteredChallenges1 = challengeRepository
                .findByLevelAndLocale("MEDIUM", locale);
        Flux<ChallengeDocument> filteredChallenges2 = challengeRepository
                .findByLevelAndLocale("EASY", locale);

        StepVerifier.create(filteredChallenges1)
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(filteredChallenges2)
                .expectNextCount(1)
                .verifyComplete();
    }

}