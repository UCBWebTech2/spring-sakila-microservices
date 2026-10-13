package com.ucb.sakila.businessservice.store;

import com.ucb.sakila.businessservice.shared.PageResult;
import com.ucb.sakila.businessservice.staff.Staff;
import com.ucb.sakila.businessservice.staff.StaffRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final StaffRepository staffRepository;

    @QueryMapping
    public Store store(@Argument Integer id) {
        return storeService.findOneById(id);
    }

    @QueryMapping
    public PageResult<Store> stores(@Argument Integer page, @Argument Integer limit) {
        return storeService.findAll(page, limit);
    }

    @MutationMapping
    public Store createStore(@Argument CreateStoreInput input) {
        return storeService.create(input);
    }

    @MutationMapping
    public Store updateStore(@Argument Integer id, @Argument UpdateStoreInput input) {
        return storeService.update(id, input);
    }

    @MutationMapping
    public Boolean removeStore(@Argument Integer id) {
        storeService.remove(id);
        return true;
    }

    // managerStaffId es columna plana; @BatchMapping evita N+1 al resolverla.
    @BatchMapping
    public Map<Store, Staff> manager(List<Store> stores) {
        List<Integer> managerIds = stores.stream().map(Store::getManagerStaffId).distinct().toList();
        Map<Integer, Staff> staffById = staffRepository.findAllById(managerIds).stream()
                .collect(Collectors.toMap(Staff::getId, s -> s));
        return stores.stream().collect(Collectors.toMap(s -> s, s -> staffById.get(s.getManagerStaffId())));
    }
}
