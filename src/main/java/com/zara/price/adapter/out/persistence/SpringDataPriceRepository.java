package com.zara.price.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataPriceRepository extends JpaRepository<PriceEntity, Long> {

    @Query("""
            select p from PriceEntity p
            where p.brandId = :brandId
              and p.productId = :productId
              and :applicationDate between p.startDate and p.endDate
            order by p.priority desc, p.startDate desc, p.id desc
            """)
    List<PriceEntity> findApplicable(@Param("brandId") long brandId,
                                     @Param("productId") long productId,
                                     @Param("applicationDate") LocalDateTime applicationDate,
                                     Limit limit);
}
