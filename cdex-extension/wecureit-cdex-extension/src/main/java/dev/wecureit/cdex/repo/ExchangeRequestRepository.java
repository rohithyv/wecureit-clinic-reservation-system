package dev.wecureit.cdex.repo;
import dev.wecureit.cdex.model.ExchangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest,String> {  }
