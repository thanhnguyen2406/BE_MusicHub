package MusicHub.service.interf;

import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.UserDTO;

public interface IUserService {
    ResponseAPI<Void> createPatient(String email, String name);
    ResponseAPI<UserDTO> getMyInfo();
}
