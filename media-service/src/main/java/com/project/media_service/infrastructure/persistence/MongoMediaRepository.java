package com.project.media_service.infrastructure.persistence;

import com.project.media_service.domain.model.Media;
import com.project.media_service.domain.model.MediaId;
import com.project.media_service.domain.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoMediaRepository implements MediaRepository {

    private final SpringMediaRepository springMediaRepository;

    @Override
    public Media save(Media media) {
        MediaDocument doc = MediaMapper.toDocument(media);
        MediaDocument saved = springMediaRepository.save(doc);
        return MediaMapper.toDomain(saved);
    }

    @Override
    public List<Media> saveAll(List<Media> mediaList) {
        List<MediaDocument> docs = mediaList.stream()
                .map(MediaMapper::toDocument)
                .toList();
        return springMediaRepository.saveAll(docs).stream()
                .map(MediaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Media> findById(MediaId id) {
        return springMediaRepository.findById(id.getId())
                .map(MediaMapper::toDomain);
    }

    @Override
    public List<Media> findAllByIssueId(String issueId) {
        return springMediaRepository.findAllByIssueId(issueId).stream()
                .map(MediaMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(MediaId id) {
        return springMediaRepository.existsById(id.getId());
    }

    @Override
    public void deleteById(MediaId id) {
        springMediaRepository.deleteById(id.getId());
    }

    @Override
    public void deleteByIssueId(String issueId) {
        springMediaRepository.deleteAll(
                springMediaRepository.findAllByIssueId(issueId)
        );
    }
}
