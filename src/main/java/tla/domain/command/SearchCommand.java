package tla.domain.command;

import java.lang.annotation.Annotation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.Setter;
import tla.domain.dto.meta.DocumentDto;
import tla.domain.model.meta.AbstractBTSBaseClass;
import tla.domain.model.meta.TLADTO;

/**
 * Implementing subclasses should either have a {@link TLADTO} annotation
 * with the same DTO class as its value as with which it (the subclass)
 * is typed, <i>or</i> override {@link #getDTOClass()} to return it
 * (the DTO class).
 */
@Getter
@Setter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class SearchCommand<T extends AbstractBTSBaseClass> {

    private String sort;

    /**
     * Retrieve the DTO model class targeted by this search command.
     * Does so by going though the search command class's annotations
     * and see whether there's a {@link TLADTO} annotation with the
     * DTO model class as its value.
     *
     * @return DTO class of which this search command is supposed to produce instances.
     */
    public Class<? extends AbstractBTSBaseClass> getDTOClass() {
        for (Annotation a : this.getClass().getAnnotations()) {
            if (a instanceof TLADTO) {
                return ((TLADTO) a).value();
            }
        }
        return DocumentDto.class;
    }

    public void setDTOClass(Class<? extends AbstractBTSBaseClass> dtoclass) {}

}