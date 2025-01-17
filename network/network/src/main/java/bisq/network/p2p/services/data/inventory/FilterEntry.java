/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.network.p2p.services.data.inventory;

import bisq.common.proto.Proto;
import bisq.common.validation.NetworkDataValidation;
import com.google.protobuf.ByteString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.Arrays;

@Getter
@ToString
@EqualsAndHashCode
public final class FilterEntry implements Proto, Comparable<FilterEntry> {
    private final byte[] hash;
    private final int sequenceNumber;

    public FilterEntry(byte[] hash, int sequenceNumber) {
        this.hash = hash;
        this.sequenceNumber = sequenceNumber;

        NetworkDataValidation.validateHash(hash);
    }

    public bisq.network.protobuf.FilterEntry toProto() {
        return bisq.network.protobuf.FilterEntry.newBuilder()
                .setHash(ByteString.copyFrom(hash))
                .setSequenceNumber(sequenceNumber)
                .build();
    }

    public static FilterEntry fromProto(bisq.network.protobuf.FilterEntry proto) {
        return new FilterEntry(proto.getHash().toByteArray(), proto.getSequenceNumber());
    }

    @Override
    public int compareTo(@Nonnull FilterEntry o) {
        return Arrays.compare(hash, o.getHash());
    }
}