package bisq.support.mediation;

import bisq.common.observable.Observable;
import bisq.common.proto.Proto;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MediationCase implements Proto {
    @EqualsAndHashCode.Include
    private final MediationRequest mediationRequest;
    private final long requestDate;
    private final Observable<Boolean> isClosed = new Observable<>();

    public MediationCase(MediationRequest mediationRequest) {
        this(mediationRequest, new Date().getTime(), false);
    }

    private MediationCase(MediationRequest mediationRequest, long requestDate, boolean isClosed) {
        this.mediationRequest = mediationRequest;
        this.requestDate = requestDate;
        this.isClosed.set(isClosed);
    }

    @Override
    public bisq.support.protobuf.MediationCase toProto() {
        return bisq.support.protobuf.MediationCase.newBuilder()
                .setMediationRequest(mediationRequest.toMediationRequestProto())
                .setRequestDate(requestDate)
                .setIsClosed(isClosed.get())
                .build();
    }

    public static MediationCase fromProto(bisq.support.protobuf.MediationCase proto) {
        return new MediationCase(MediationRequest.fromProto(proto.getMediationRequest()),
                proto.getRequestDate(),
                proto.getIsClosed());
    }

    public void setClosed(boolean closed) {
        isClosed.set(closed);
    }
}
