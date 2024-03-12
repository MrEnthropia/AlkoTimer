package ru.MrEntropy.AlkoBot.AlkoTimer.DAO;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.MrEntropy.AlkoBot.AlkoTimer.Model.User;

@Repository
public interface UserRepository extends CrudRepository<User,Long> {
}
