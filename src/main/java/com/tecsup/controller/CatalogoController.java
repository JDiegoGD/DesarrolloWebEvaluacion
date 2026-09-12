package com.tecsup.controller;

import com.tecsup.model.TipoDocumento;
import com.tecsup.model.Ubigeo;
import com.tecsup.repository.TipoDocumentoRepository;
import com.tecsup.repository.UbigeoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    @Autowired private TipoDocumentoRepository tipoDocRepo;
    @Autowired private UbigeoRepository ubigeoRepo;

    @GetMapping("/tipo-documento")
    public ResponseEntity<List<TipoDocumento>> listarTipos() {
        return ResponseEntity.ok(tipoDocRepo.findAll());
    }

    @PostMapping("/tipo-documento")
    public ResponseEntity<TipoDocumento> crearTipo(@RequestBody TipoDocumento td) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoDocRepo.save(td));
    }

    @GetMapping("/ubigeo")
    public ResponseEntity<List<Ubigeo>> listarUbigeos() {
        return ResponseEntity.ok(ubigeoRepo.findAll());
    }

    @PostMapping("/ubigeo")
    public ResponseEntity<Ubigeo> crearUbigeo(@RequestBody Ubigeo u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ubigeoRepo.save(u));
    }
}