package io.github.opensabe.mapstruct;

import io.github.opensabe.mapstruct.core.ObjectConverter;
import io.github.opensabe.mapstruct.core.RegisterRepository;
import io.github.opensabe.mapstruct.core.SelfCopyMapper;
import org.mapstruct.Mapper;

@RegisterRepository("grade")
@Mapper(uses = ObjectConverter.class)
public interface GradeMapper extends SelfCopyMapper<Grade> {
}
