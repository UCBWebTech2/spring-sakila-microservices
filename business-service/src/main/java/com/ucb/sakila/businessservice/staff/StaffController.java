package com.ucb.sakila.businessservice.staff;

import com.ucb.sakila.businessservice.shared.PageResult;
import com.ucb.sakila.businessservice.store.Store;
import com.ucb.sakila.businessservice.store.StoreRepository;
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
public class StaffController {

    private final StaffService staffService;
    private final StoreRepository storeRepository;

    @QueryMapping
    public Staff staff(@Argument Integer id) {
        return staffService.findOneById(id);
    }

    @QueryMapping
    public PageResult<Staff> staffs(@Argument Integer page, @Argument Integer limit) {
        return staffService.findAll(page, limit);
    }

    @MutationMapping
    public Staff createStaff(@Argument CreateStaffInput input) {
        return staffService.create(input);
    }

    @MutationMapping
    public Staff updateStaff(@Argument Integer id, @Argument UpdateStaffInput input) {
        return staffService.update(id, input);
    }

    @MutationMapping
    public Boolean removeStaff(@Argument Integer id) {
        staffService.remove(id);
        return true;
    }

    @MutationMapping
    public Staff staffLogin(@Argument StaffLoginInput input) {
        return staffService.verifyCredentials(input.username(), input.password());
    }

    // @BatchMapping evita N+1 y LazyInitializationException con open-in-view:false.
    @BatchMapping
    public Map<Staff, Store> store(List<Staff> staffs) {
        List<Integer> storeIds = staffs.stream().map(s -> s.getStore().getId()).distinct().toList();
        Map<Integer, Store> storesById = storeRepository.findAllById(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, s -> s));
        return staffs.stream().collect(Collectors.toMap(s -> s, s -> storesById.get(s.getStore().getId())));
    }
}
