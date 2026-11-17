package com.javanauta.agendadortarefas.business;

import com.javanauta.agendadortarefas.business.dto.TarefasDTO;
import com.javanauta.agendadortarefas.business.mapper.TarefaUpdateConverter;
import com.javanauta.agendadortarefas.business.mapper.TarefasConverter;
import com.javanauta.agendadortarefas.infrastructure.entity.TarefasEntity;
import com.javanauta.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import com.javanauta.agendadortarefas.infrastructure.exceptions.ResourceNotFoundException;
import com.javanauta.agendadortarefas.infrastructure.repository.TarefasRepository;
import com.javanauta.agendadortarefas.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefasService {

    private final TarefasRepository tarefasRepository;
    private final TarefasConverter tarefaConverter;
    private final JwtUtil jwtUtil;
    private final TarefaUpdateConverter tarefaUpdateConverter;

    public TarefasDTO gravarTarefa(String token, TarefasDTO tarefasDTO) {
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        tarefasDTO.setDataCriacao(LocalDateTime.now());
        tarefasDTO.setStatusNotificacaoEnum(StatusNotificacaoEnum.PENDENTE);
        tarefasDTO.setEmailUsuario(email);
        TarefasEntity entity = tarefaConverter.paraTarefaEntity(tarefasDTO);

        return tarefaConverter.paraTarefaDTO(tarefasRepository.save(entity));
    }

    public List<TarefasDTO> buscaTarefasPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {
        return tarefaConverter.paraListaTarefasDTO(tarefasRepository.findByDataEventoBetween(dataInicial, dataFinal));
    }

    public List<TarefasDTO> buscaTarefasPorEmail(String token) {
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        return tarefaConverter.paraListaTarefasDTO(tarefasRepository.findByEmailUsuario(email));
    }

    public void deletaTarefaPorId(String id) {
        try {
            tarefasRepository.deleteById(id);

        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Id não encontrado: " + id, e.getCause());
        }
    }

    public TarefasDTO alteraStatusTarefa(StatusNotificacaoEnum statusNotificacaoEnum, String id){
        TarefasEntity entity = tarefasRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("id da tarefa não encontrado: " + id));

        entity.setStatusNotificacaoEnum(statusNotificacaoEnum);
        return tarefaConverter.paraTarefaDTO(tarefasRepository.save(entity));
    }

    public TarefasDTO updateTarefa(TarefasDTO tarefasDTO, String id){
        TarefasEntity entity = tarefasRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("id não encontrado" + id));

        tarefaUpdateConverter.updateTarefas(tarefasDTO, entity);
        return tarefaConverter.paraTarefaDTO(tarefasRepository.save(entity));
    }
}
