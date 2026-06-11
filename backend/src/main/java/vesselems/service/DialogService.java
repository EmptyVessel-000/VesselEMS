package vesselems.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vesselems.model.Dialog;
import vesselems.repository.DialogRepository;

@Service
public class DialogService {

    private final DialogRepository dialogRepository;

    public DialogService(DialogRepository dialogRepository) {
        this.dialogRepository = dialogRepository;
    }

    public List<Dialog> listDialogs() {
        return dialogRepository.findAll();
    }

    public Dialog getById(Long id) {
        return dialogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("记录不存在"));
    }

    public void deleteById(Long id) {
        dialogRepository.deleteById(id);
    }
}