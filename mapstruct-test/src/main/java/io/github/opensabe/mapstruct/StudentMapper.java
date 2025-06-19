package io.github.opensabe.mapstruct;

import io.github.opensabe.mapstruct.core.FromMapMapper;
import io.github.opensabe.mapstruct.core.ObjectConverter;
import io.github.opensabe.mapstruct.core.RegisterRepository;
import org.mapstruct.Mapper;

@RegisterRepository("Student")
@Mapper(uses = ObjectConverter.class)
public interface StudentMapper extends FromMapMapper<Student> {
}
