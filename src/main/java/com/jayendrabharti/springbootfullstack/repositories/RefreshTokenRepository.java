package com.jayendrabharti.springbootfullstack.repositories;

import com.jayendrabharti.springbootfullstack.models.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

	Optional<RefreshToken> findByToken(String token);

	void deleteByUserId(String userId);

}
