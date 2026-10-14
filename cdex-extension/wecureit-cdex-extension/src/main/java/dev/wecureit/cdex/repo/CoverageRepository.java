package dev.wecureit.cdex.repo;
import dev.wecureit.cdex.model.Coverage;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CoverageRepository extends JpaRepository<Coverage,String> { java.util.List<Coverage> findByMemberId(String memberId); }
