package com.itachallenge.user.service;

import com.itachallenge.user.document.UserSolutionDocument;
import com.itachallenge.user.dtos.SolutionUserDto;
import com.itachallenge.user.dtos.UserScoreDto;
import com.itachallenge.user.dtos.UserSolutionDto;
import com.itachallenge.user.dtos.UserSolutionScoreDto;
import reactor.core.publisher.Mono;

public interface IUserSolutionService {

    Mono<SolutionUserDto<UserScoreDto>> getChallengeById(String id, String idChallenge, String idLanguage);
    Mono<UserSolutionScoreDto> addSolution(UserSolutionDto userSolutionDto);
    Mono<UserSolutionDocument> markAsBookmarked(String uuidChallenge, String uuidLanguage, String uuidUser, boolean bookmarked);

}
