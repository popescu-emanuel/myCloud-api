package fmi.unibuc.ro.mycloudapi.repositories;

import fmi.unibuc.ro.mycloudapi.model.SizePlan;
import fmi.unibuc.ro.mycloudapi.model.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizePlanRepository extends JpaRepository<SizePlan, Long> {
    SizePlan findSizePlanByType(SubscriptionType subscriptionType);
}
