package AuctionUser.application;

import AuctionUser.domain.IApplicationUserRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplicationUserService {
    private final IApplicationUserRepository applicationUserRepository;


    public ApplicationUserService(IApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }


}
